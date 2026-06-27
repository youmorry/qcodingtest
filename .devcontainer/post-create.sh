#!/bin/bash
set -e

bash -c 'source "${SDKMAN_DIR:-/usr/local/sdkman}/bin/sdkman-init.sh" && sdk install kotlin 2.4.0'

# Install Claude Code (native install, recommended)
curl -fsSL https://claude.ai/install.sh | bash

echo "Done! Development environment is ready."
