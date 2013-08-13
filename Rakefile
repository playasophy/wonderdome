# Rake tasks to build and deploy the wonderdome.
#
# Author:: Greg Look

require 'rake/clean'
require 'rakejava'


### BUILD CONFIG ###

LIBRARY_NAME = 'wonderdome'

# package directories
BUILD_DIR = 'build'
LIB_DIR   = 'lib'
SRC_DIR   = 'src'
WEB_DIR   = 'web'

# compiler paths
SKETCHBOOK_PATH = "#{ENV['HOME']}/sketchbook"
processing_home = "#{ENV['HOME']}/processing"
processing_cmd = nil



### DEPLOYMENT CONFIG ###

SSH_USER       = "wonder@wonderdome"
SSH_PORT       = "22"
RSYNC_DELETE   = true
RSYNC_ARGS     = ""

DEPLOY_ROOT    = "~"



### UTILITY METHODS ###

# Locates a command and ensures it is executable.
def locate_command(name, dir=nil, msg="")
  command = nil

  if File.directory? dir
    command = "#{dir}/#{name}"
  else
    path = `which #{name}`
    command = path if $?.success?
  end

  fail "Unable to locate '#{name}' command! #{msg}" if command.nil?
  fail "Command does not exist: #{command} #{msg}" unless File.exist? command
  fail "Command is not executable: #{command} #{msg}" unless File.executable? command

  command
end



### PREPARATION TASKS ###

# Prepare the build directory.
#directory BUILD_DIR.to_s
CLOBBER << BUILD_DIR


namespace :processing do

  desc "locate Processing directory and compiler"
  task :configure do
    processing_home = ENV['PROCESSING_HOME'] || processing_home
    processing_cmd = locate_command 'processing-java', processing_home, "Please install Processing in your home directory or add it to your $PATH."
    puts "Found Processing compiler: #{processing_cmd}"
    processing_home = File.dirname(processing_cmd)
  end

end


namespace :lib do

  classes_dir = "#{BUILD_DIR}/classes"
  lib_dir = "#{BUILD_DIR}/lib/#{LIBRARY_NAME}"
  lib_src_dir  = "#{lib_dir}/src"
  lib_doc_dir  = "#{lib_dir}/doc"
  lib_lib_dir  = "#{lib_dir}/library"
  lib_jar_file = "#{lib_dir}/library/#{LIBRARY_NAME}.jar"

  # library directories
  directory classes_dir
  directory lib_dir
  directory lib_src_dir
  directory lib_doc_dir
  directory lib_lib_dir

  CLEAN << classes_dir

  desc "check for required Processing libraries"
  task :dependencies do
    libs = ['udp', 'PixelPusher']
    missing = libs.reject {|lib| File.directory? "#{LIB_DIR}/#{lib}" }
    fail "Missing Processing libraries: #{missing.join(', ')}. Install them locally in '#{LIB_DIR}'." unless missing.empty?
  end

  desc "compile Java source files"
  javac :compile => [:dependencies, 'processing:configure', classes_dir] do |t|
    t.classpath << "#{processing_home}/core/library/core.jar"
    t.classpath << FileList["#{LIB_DIR}/**/*.jar"]

    t.src << Sources[SRC_DIR, "**/*.java"]
    t.dest = classes_dir
    t.dest_ver = '1.6'
  end

  jar lib_jar_file => [:compile, lib_lib_dir] do |t|
    t.files << JarFiles[classes_dir, "**/*.class"]
  end

  desc "build library jar file"
  task :jar => lib_jar_file

  desc "copy library sources to output"
  task :copy_src => lib_src_dir do
    opts = %w{
      --recursive
      --archive
      --delete
      --delete-excluded
      --exclude=library.properties
      --verbose
    }

    puts `rsync #{opts.join(' ')} #{SRC_DIR}/ #{lib_src_dir}/`
  end

  desc "generate library documentation"
  task :doc => lib_doc_dir do
    # TODO: javadoc to build/lib/wonderdome/doc/...
  end

  desc "build all library components"
  task :build => [:jar, :copy_src, :doc] do
    cp "#{SRC_DIR}/library.properties", lib_dir
  end

  desc "construct a zip of the library for distribution"
  task :release => :build do
    `cd #{File.dirname(lib_dir)} && zip -r #{LIBRARY_NAME}.zip #{File.basename(lib_dir)}`
  end

  desc "link to the compiled library in the user's sketchbook"
  task :install => :build do
    sketchbook_lib_dir = "#{SKETCHBOOK_PATH}/libraries"
    mkdir_p sketchbook_lib_dir
    ln_sf File.expand_path(lib_dir), "#{sketchbook_lib_dir}/#{LIBRARY_NAME}"
  end

end



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

task :default => 'lib:release'
