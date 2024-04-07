# D&D Assistant

## Setup

```bash
./gradlew fatJar
java -jar assistant-cli/build/libs/assistant-cli-fat-*.jar complete -c assistant-cli/dndrc.yaml 
```

## Design

### Configuration and State

One config file, read at boot containing all secrets, variables and paths.
By using a different config file, the program can be used for different games.

By default, it will use `$PWD/dndrc.yaml`.

### Modules

Folder structure is arbitrary, but files are always Markdown and end on .md.
Files can be included in other files using the `include` directive.

```md
# Background 

!include: ./path/to/file.md

## More stuff

...
```

Paths are relative to the file they are included in.
When absolute the root is the root configured in the configuration file.

### Chats

When running the program, you will choose a chat name, this will determine the folder and file.
Chats are saved as markdown.

It will not allow you to overwrite an existing file.