# Rake tasks to build and deploy the wonderdome.
#
# Author:: Greg Look



### BUILD CONFIG ###

build_dir = 'build'
lib_dir   = 'lib'
src_dir   = 'src'
web_dir   = 'web'

processing_cmd = nil



### DEPLOYMENT CONFIG ###

ssh_user       = "wonder@wonderdome"
ssh_port       = "22"
rsync_delete   = true
rsync_args     = ""

deploy_root    = "~"



### TASKS ###

desc "remove the build directory"
task :clean do
  rm_rf build_dir
end


# Prepare the build directory.
directory build_dir


# Locate the executable processing-java command.
task :check_processing do
  processing_cmd = `which processing-java`
  if not $?.success?
    processing_home = File.join(ENV['HOME'], 'processing')
    processing_cmd = File.join(processing_home, 'processing-java') if File.directory? processing_home
  end

  if processing_cmd.nil?
    raise "Unable to locate processing-java command! Please install Processing in your home directory or add it to your $PATH."
  elsif not File.executable?(processing_cmd)
    raise "Processing command is not executable or nonexistent: #{processing_cmd}"
  else
    puts "Found Processing compiler: #{processing_cmd}"
  end
end


# Check for required Processing libraries.
task :check_libraries do
  lib_path = File.expand_path(File.join('..', lib_dir), __FILE__);
  libs = ['udp', 'PixelPusher']

  missing_libs = libs.reject do |lib| File.directory?(File.join(lib_path, lib)) end
  unless missing_libs.empty?
    raise "Missing Processing libraries: #{missing_libs.join(', ')}. Install them locally in '#{lib_path}'."
  end
end


# TODO: Compile Processing library.


# TODO: Link Processing libraries into the user's sketchbook.


# TODO: Build sinatra app(?)


# TODO: Deploy via rsync.


task :default => [:check_processing, :check_libraries, build_dir]
