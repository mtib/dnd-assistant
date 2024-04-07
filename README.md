# D&D Assistant

This project is a CLI tool to help Dungeon Masters prepare for their sessions by allowing OpenAI Chat Completions to _fill in the blanks_.

## Setup

1. Follow build instructions to get an executable jar file.
2. Create a configuration yaml file.
   ```yaml
   openAiToken: "<your-openai-token>"
   openAiModel: "gpt-4-0125-preview" # or any other model
   projectRoot: "/absolute/path/to/your/markdown/tree"
   ```

## Usage

### Markdown Extensions

To use the assistant, you need to create a markdown file with prompts for the AI to fill in.

For example:

#### template/bbeg.md

```markdown
### Big Bad Evil Guy

!prompt: Add a short visual description including species and class.

#### History

!prompt: Add a short history of interactions with the world.
```

#### session/plan.md

```markdown
# Session Plan

## Setting

This session starts in a generic small village near a forest.

!prompt: Add a short description of the setting.

## Introduction

!prompt: Add a short introduction to the session.

## Encounters

!include: ../template/bbeg.md

!prompt: Add a short description of the first encounter.
```

#### Extensions

- `!prompt: <prompt>` - Prompts the AI to fill in the blank.
- `!include: <path>` - Includes another markdown file.

### Commands

The exposed commands in the assitant-cli subproject are:

- `complete` - Allows to prompt the AI using CLI args (mostly for verifying the configuration)
- `create <file>` - Prompts the AI to fill in the blanks in the given file (`file` is relative to the `projectRoot` in the configuration)

## Build

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