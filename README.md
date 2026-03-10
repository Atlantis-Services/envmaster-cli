# envmaster

The official CLI for [EnvMaster](https://envmaster.dev) — securely manage environment variables across projects and teams.

A free or paid EnvMaster account is required to use this tool.

## Installation

Build from source (JDK 17+ and Gradle required):

```bash
git clone https://github.com/Atlantis-Services/envmaster-cli
cd envmaster-cli
./gradlew installDist
```

Add `build/install/envmaster/bin` to your `PATH`.

## Quick start

```bash
envmanager login           # authenticate via browser
envmanager init            # link current directory to a project & environment
envmanager run -- <cmd>    # run any command with env vars injected
```

## Commands

| Command                          | Description |
|----------------------------------|-------------|
| `login [--profile]`              | Authenticate via browser, save credentials locally |
| `logout [--profile]`             | Remove stored credentials |
| `whoami [--profile]`             | Show current authenticated user |
| `init [--profile]`               | Interactively set up `.envmanager` in current directory |
| `run -- <cmd>`                   | Inject env vars and run a command |
| `project [id\|name\|--list]`     | Set or show the active project |
| `environment [id\|name\|--list]` | Set or show the active environment |
| `profile [list\|use\|remove]`    | Manage authentication profiles |

Run `envmanager <command> --help` for full flag details.

## Configuration

`envmanager init` creates two files in your project directory:

| File | Purpose | Commit? |
|------|---------|---------|
| `.envmanager` | Project and environment selection | ✅ Yes |
| `.envmanager.local` | Personal profile override | ❌ No |

Add `.envmanager.local` to your `.gitignore`. Credentials are stored globally in `~/.envmanager/profiles.json`.

## Multiple profiles

```bash
envmanager login --profile work
envmanager login --profile personal
envmanager profile use work        # switch active profile
```

## Roadmap

- [ ] Homebrew
- [ ] Scoop
- [ ] apt / deb
- [ ] rpm
- [ ] AUR (Arch)
- [ ] winget

## Contributing

Open an issue before submitting a PR for large changes. Fork the repo, create a branch off `master`, and open a pull request.

## License

[Apache License 2.0](LICENSE) — Copyright (c) 2026 Atlantis Services