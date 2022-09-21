# Self-hosting instructions!

As ProxyFox is privacy-focused, giving instructions on setting up your own instance is a must

## 1. Prerequisites

### 1a. Java Development Kit

ProxyFox runs on top of Java 17, so you need to install the proper JDK, we recommend [Adoptium OpenJDK](https://adoptium.net/)

### 1b. MongoDB (Optional, but recommended)

For the most compatible database implementation, it's recommended to [install MongoDB](https://www.mongodb.com/docs/manual/installation/)

### 1c. Environment Variables

ProxyFox requires an environment variable set, called `PROXYFOX_KEY`, that contains the token for the bot

## 2. Building

ProxyFox uses a Gradle build system. You can follow the building instructions for your platform

### 2a. Windows (Command Prompt)

```bash
gradlew build
```

### 2b. Windows (PowerShell), Linux (Bash, Shell), Mac (Terminal)

```bash
./gradlew build
```

### 2c. Intellij IDEA

Run the `build` Gradle task

## 3 Running ProxyFox

Take the `proxyfox.jar` (name may vary), put it into a separate folder, and run the following command
(Replace `proxyfox.jar` with the name of the file)
```batch
java -jar proxyfox.jar
```

## Additional Notes

- If you encounter any problems with any of the steps, join [our discord server](https://discord.gg/q3yF8ay9V7) and open
a support thread.