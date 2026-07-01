FROM mcr.microsoft.com/devcontainers/java:3-25
# 先に vscode 所有で用意しないと root 所有で作られ、vscode で動く Claude Code が書き込めない。
RUN mkdir -p /home/vscode/.claude && chown -R vscode:vscode /home/vscode/.claude
