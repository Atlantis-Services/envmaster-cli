# envmaster

The official CLI for [EnvMaster](https://envmaster.dev) — securely manage environment variables across projects and teams.

A free or paid EnvMaster account is required to use this tool.

## Installation

**macOS / Linux**
```bash
curl -fsSL https://raw.githubusercontent.com/Atlantis-Services/envmaster-cli/master/install.sh | sh
```

**Windows** (PowerShell)
```powershell
irm https://raw.githubusercontent.com/Atlantis-Services/envmaster-cli/master/install.ps1 | iex
```

To uninstall: `envmaster uninstall`

## Quick start

```bash
envmaster login           # authenticate via browser
envmaster init            # link current directory to a project & environment
envmaster run -- <cmd>    # run any command with env vars injected
```

## Commands

| Command                          | Description |
|----------------------------------|-------------|
| `login [--profile]`              | Authenticate via browser, save credentials locally |
| `logout [--profile]`             | Remove stored credentials |
| `whoami [--profile]`             | Show current authenticated user |
| `init [--profile]`               | Interactively set up `.envmaster` in current directory |
| `run -- <cmd>`                   | Inject env vars and run a command |
| `project [id\|name\|--list]`     | Set or show the active project |
| `environment [id\|name\|--list]` | Set or show the active environment |
| `profile [list\|use\|remove]`    | Manage authentication profiles |
| `uninstall` | Remove envmaster from your system |

Run `envmaster <command> --help` for full flag details.

## Configuration

`envmaster init` creates two files in your project directory:

| File | Purpose | Commit? |
|------|---------|---------|
| `.envmaster` | Project and environment selection | ✅ Yes |
| `.envmaster.local` | Personal profile override | ❌ No |

Add `.envmaster.local` to your `.gitignore`. Credentials are stored globally in `~/.envmaster/profiles.json`.

## Multiple profiles

```bash
envmaster login --profile work
envmaster login --profile personal
envmaster profile use work        # switch active profile
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