/*
 * Copyright (c) 2023, The ProxyFox Group
 *
 * This Source Code is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package dev.proxyfox.patch

import org.quiltmc.loader.api.Version
import org.quiltmc.loader.impl.FormattedException
import org.quiltmc.loader.impl.QuiltLoaderImpl
import org.quiltmc.loader.impl.entrypoint.GameTransformer
import org.quiltmc.loader.impl.game.GameProvider
import org.quiltmc.loader.impl.game.GameProvider.BuiltinMod
import org.quiltmc.loader.impl.launch.common.QuiltLauncher
import org.quiltmc.loader.impl.metadata.qmj.V1ModMetadataBuilder
import org.quiltmc.loader.impl.util.Arguments
import org.quiltmc.loader.impl.util.ExceptionUtil
import org.quiltmc.loader.impl.util.SystemProperties
import org.quiltmc.loader.impl.util.log.ConsoleLogHandler
import org.quiltmc.loader.impl.util.log.Log
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.notExists
import kotlin.io.path.writeText

// Created 2023-05-01T19:51:55

/**
 * Partial copy of the Minecraft [GameProvider] adapted for use with ProxyFox.
 *
 * @author KJP12
 * @since 2.0.8
 **/
class ProxyFoxProvider : GameProvider {
    private val TRANSFORMER = GameTransformer()

    private var arguments: Arguments? = null
    private lateinit var entrypoint: String
    private lateinit var appJar: Path
    private lateinit var miscJars: Set<Path>

    override fun getGameId() = "proxyfox"

    override fun getGameName() = "ProxyFox"

    override fun getRawGameVersion() = "0.0.0"

    override fun getNormalizedGameVersion() = "0.0.0"

    override fun getBuiltinMods() = setOf(BuiltinMod(listOf(appJar), V1ModMetadataBuilder().apply {
        id = gameId
        group = "builtin"
        version = Version.of(normalizedGameVersion)
        name = gameName
    }.build()))

    override fun getEntrypoint() = entrypoint

    override fun getLaunchDirectory() = Path.of(".")

    override fun isObfuscated() = false

    override fun requiresUrlClassLoader() = false

    override fun isEnabled() = true

    override fun locateGame(launcher: QuiltLauncher, args: Array<out String>): Boolean {
        this.arguments = Arguments().apply { parse(args) }

        try {
            val jar = System.getProperty(SystemProperties.GAME_JAR_PATH)
            val lookupPaths: List<Path>

            if (jar != null) {
                val path = Paths.get(jar).toAbsolutePath().normalize()

                if (path.notExists()) {
                    throw RuntimeException("ProxyFox jar $path configured through ${SystemProperties.GAME_JAR_PATH} system property doesn't exist!")
                }

                lookupPaths = path + launcher.classPath
            } else {
                lookupPaths = launcher.classPath
            }

            val classifier = BudgetLibClassifier(ProxyFoxLibrary::class.java)
            classifier.process(lookupPaths, launcher.environmentType)

            appJar = classifier.getOrigin(ProxyFoxLibrary.PROXY_FOX) ?: return false

            miscJars = classifier.unmatched

            entrypoint = classifier.getClassName(ProxyFoxLibrary.PROXY_FOX)
        } catch (e: IOException) {
            throw ExceptionUtil.wrap(e)
        }

        QuiltLoaderImpl.INSTANCE.objectShare.put("fabric-loader:inputGameJar", appJar)

        return true
    }

    override fun initialize(launcher: QuiltLauncher?) {
        // Ideally, use SLF4J, but we currently don't test for that.
        Log.init(ConsoleLogHandler(), true)

        val path = Files.createTempFile("Hacky-Classpath", ".txt").toAbsolutePath()
        path.writeText("$appJar${File.pathSeparator}${miscJars.joinToString(File.pathSeparator)}")

        // This is honestly stupidly hacky.
        // There's no other workaround available that I can see, so
        // you will have to deal with this.
        System.setProperty(SystemProperties.REMAP_CLASSPATH_FILE, path.toString())

        TRANSFORMER.locateEntrypoints(launcher, appJar)
    }

    override fun getEntrypointTransformer() = TRANSFORMER

    override fun unlockClassPath(launcher: QuiltLauncher) {
        launcher.addToClassPath(appJar)
        for (path in miscJars) {
            launcher.addToClassPath(path)
        }
    }

    override fun launch(loader: ClassLoader) {
        try {
            val c = loader.loadClass(entrypoint)
            val m = c.getMethod("main", Array<String>::class.java)
            m.invoke(null, arguments!!.toArray() as Any)
        } catch (e: InvocationTargetException) {
            throw FormattedException("ProxyFox has crashed", e.cause)
        } catch (e: ReflectiveOperationException) {
            throw FormattedException("Failed to start ProxyFox", e)
        }
    }

    override fun getArguments() = arguments

    override fun getLaunchArguments(sanitize: Boolean): Array<String> = arguments?.toArray() ?: emptyArray()
}