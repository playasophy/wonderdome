# Rake tasks to build and deploy the wonderdome.
#
# Author:: Greg Look

require 'pathname'


### BUILD CONFIG ###

library_name = 'wonderdome'

# package directory names
build_dir = 'build'
lib_dir   = 'lib'
src_dir   = 'src'
web_dir   = 'web'

# package directory paths
pkg_root = Pathname.new('.').expand_path
build_path = pkg_root + build_dir
lib_path   = pkg_root + lib_dir
src_path   = pkg_root + src_dir
web_path   = pkg_root + web_dir

# compiler paths
java_home = nil
sketchbook_path = Pathname.new(ENV['HOME']).join('sketchbook')
processing_home = Pathname.new(ENV['HOME']).join('processing')
processing_cmd = nil



### DEPLOYMENT CONFIG ###

ssh_user       = "wonder@wonderdome"
ssh_port       = "22"
rsync_delete   = true
rsync_args     = ""

deploy_root    = "~"



### UTILITY METHODS ###

# Locates a command and ensures it is executable.
def locate_command(name, dir=nil, msg="")
  command = nil

  if File.directory? dir
    command = Pathname.new(dir).join(name)
  else
    path = `which #{name}`
    command = $?.success? && Pathname.new(path) || nil
  end

  raise "Unable to locate '#{name}' command! #{msg}" if command.nil?
  raise "Command does not exist: #{command} #{msg}" unless command.exist?
  raise "Command is not executable: #{command} #{msg}" unless command.executable?

  command
end



### PREPARATION TASKS ###

# Prepare the build directory.
directory build_dir


# Locate the Processing compiler command.
task :locate_processing do
  processing_home = ENV['PROCESSING_HOME'] || processing_home
  processing_cmd = locate_command 'processing-java', processing_home, "Please install Processing in your home directory or add it to your $PATH."
  puts "Found Processing compiler: #{processing_cmd}"
  processing_home = processing_cmd.parent
end


# Check for required Processing libraries.
task :check_libraries do
  libs = ['udp', 'PixelPusher']
  missing_libs = libs.reject do |lib| (lib_path + lib).directory? end
  unless missing_libs.empty?
    raise "Missing Processing libraries: #{missing_libs.join(', ')}. Install them locally in '#{lib_path}'."
  end
end



### COMPILATION STEPS ###

desc "copy library sources to output"
task :copy_src => build_dir do
  build_lib_src = build_path.join('lib', library_name, 'src')
  mkdir_p build_lib_src
  cp_r src_path, build_lib_src
end


desc "compile library Java sources"
task :compile => [:locate_java, :check_libraries, build_dir] do
  # compile from src_path to build/classes/...
end


desc "build Processing library"
task :build => :compile do
  # build/lib/wonderdome/library.properties
  # build/lib/wonderdome/library/...
  # build/lib/wonderdome/src/...
end


# TODO: javadoc to build/lib/wonderdome/doc/...


desc "link libraries into the user sketchbook"
task :install_libraries => :compile do
  sketchbook_lib_path = sketchbook_path + 'libraries'
  mkdir_p sketchbook_lib_path

  lib_path.children.each do |lib_path|
    target = sketchbook_lib_path + lib_path.basename
    ln_s lib_path, target
  end

  target = sketchbook_lib_path + 'wonderdome'
  ln_s (build_path + 'lib'), target
end


# TODO: Build sinatra app(?)


# TODO: Deploy via rsync.



### MISC TASKS ###

desc "remove the build directory"
task :clean do
  rm_rf build_dir
end


task :default => [:locate_java, :locate_processing, :check_libraries, build_dir]
