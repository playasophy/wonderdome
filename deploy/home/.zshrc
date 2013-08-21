# vim: ft=zsh

# standard commands
export PAGER='less'
export EDITOR='vim'

# common aliases
alias d='dirs -v'
alias ls='ls --color=auto'
alias ll='ls -lh --color=auto'
alias la='ls -lhA --color=auto'

# enable autocompletion
autoload -U compinit && compinit

# command-line prompt
autoload -U colors && colors
export PROMPT="%B%{$fg[green]%}%n%{$fg[blue]%}@%{$fg[red]%}%m %{$fg[blue]%}%c %#%{$reset_color%}%b "
