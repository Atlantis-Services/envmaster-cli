#!/bin/sh
#
# Copyright (c) 2026 Atlantis Services
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# @author Selixe
#

set -e

REPO="Atlantis-Services/envmaster-cli"
BIN_NAME="envmaster"
INSTALL_DIR="/usr/local/bin"

OS="$(uname -s)"
ARCH="$(uname -m)"

case "$OS" in
  Linux)
    ASSET="envmaster-linux-x64"
    ;;
  Darwin)
    case "$ARCH" in
      arm64) ASSET="envmaster-macos-arm64" ;;
      *)     ASSET="envmaster-macos-x64" ;;
    esac
    ;;
  *)
    echo "Unsupported OS: $OS"
    exit 1
    ;;
esac

VERSION="${1:-$(curl -fsSL "https://api.github.com/repos/$REPO/releases/latest" | grep '"tag_name"' | sed 's/.*"tag_name": *"\(.*\)".*/\1/')}"
URL="https://github.com/$REPO/releases/download/$VERSION/$ASSET"

echo "Installing envmaster $VERSION for $OS/$ARCH..."

curl -fsSL "$URL" -o "/tmp/$BIN_NAME"
chmod +x "/tmp/$BIN_NAME"
sudo mv "/tmp/$BIN_NAME" "$INSTALL_DIR/$BIN_NAME"

echo ""
echo "Installed to $INSTALL_DIR/$BIN_NAME"
echo "Run 'envmaster --help' to get started."