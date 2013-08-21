# vim: ft=zsh

# prefer executables in ~/bin
local bin_dir="$HOME/bin"
[[ -d $bin_dir ]] && path=($bin_dir $path)

# add wonder_processor sketch to the path
path=($path "$HOME/wonder_processor")

# ensure path only contains unique entries
typeset -U path
