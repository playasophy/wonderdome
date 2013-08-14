# Rake tasks to build and deploy the wonderdome.
#
# Author:: Greg Look

require 'rake/clean'
require 'rakejava'


### BUILD CONFIG ###

LIBRARY_NAME = 'wonderdome'

# package directories
BUILD_DIR  = 'build'
LIB_DIR    = 'lib'
SKETCH_DIR = 'sketches'
SRC_DIR    = 'src'
WEB_DIR    = 'web'

# processing paths
SKETCHBOOK_PATH = ENV['SKETCHBOOK_HOME'] || "#{ENV['HOME']}/sketchbook"
processing_home = ENV['PROCESSING_HOME'] || "#{ENV['HOME']}/processing"
processing_cmd = nil



### DEPLOYMENT CONFIG ###

SSH_USER    = "wonder@wonderdome"
SSH_PORT    = "22"
DEPLOY_ROOT = "~"



### UTILITY METHODS ###

# Copy files with rsync.
def rsync(src, dest, extra_opts={})
  opts = %w{
    --recursive
    --archive
    --delete
    --delete-excluded
    --verbose
  }

  opts << "--exclude=#{extra_opts[:exclude]}" if extra_opts[:exclude]

  command = "rsync #{opts.join(' ')} #{src} #{dest}"
  puts command
  puts `#{command}`
end


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



### BUILD TASKS ###

# 'clobber' should remove all generated files
CLOBBER << BUILD_DIR


namespace :processing do

  sketchbook_lib_dir = "#{SKETCHBOOK_PATH}/libraries"

  # processing directories
  directory sketchbook_lib_dir

  # desc "locate Processing directory and compiler"
  task :configure do
    processing_cmd = locate_command 'processing-java', processing_home, "Please install Processing in your home directory or add it to your $PATH."
    puts "Found Processing compiler: #{processing_cmd}"
    processing_home = File.dirname(processing_cmd)
  end

  desc "check for required Processing libraries"
  task :check_libs do
    libs = ['udp', 'PixelPusher']
    missing = libs.reject {|lib| File.directory? "#{LIB_DIR}/#{lib}" }
    fail "Missing Processing libraries: #{missing.join(', ')}. Install them locally in '#{LIB_DIR}'." unless missing.empty?
  end

  desc "link Processing libraries in user's sketchbook"
  task :link_libs => [:check_libs, sketchbook_lib_dir] do
    FileList["#{LIB_DIR}/*"].each do |lib|
      target = "#{sketchbook_lib_dir}/#{File.basename(lib)}"
      ln_s File.expand_path(lib), target unless File.exist? target
    end
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

  desc "compile library source files"
  javac :compile => ['processing:configure', 'processing:check_libs', classes_dir] do |t|
    t.classpath << "#{processing_home}/core/library/core.jar"
    t.classpath << FileList["#{LIB_DIR}/**/*.jar"]

    t.src << Sources[SRC_DIR, "**/*.java"]
    t.dest = classes_dir
    t.dest_ver = '1.6'
  end

  jar lib_jar_file => [:compile, lib_lib_dir] do |t|
    t.files << JarFiles[classes_dir, "**/*.class"]
  end

  # desc "build library jar file"
  task :jar => lib_jar_file

  # desc "copy library sources to output"
  task :copy_src => lib_src_dir do
    rsync "#{SRC_DIR}/", "#{lib_src_dir}/", exclude: 'library.properties'
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


namespace :sketch do

  bin_dir = "#{BUILD_DIR}/bin"
  sketches_build_dir = "#{BUILD_DIR}/sketches"

  # sketch directories
  directory bin_dir
  directory sketches_build_dir

  CLEAN << sketches_build_dir

  # TODO: run a sketch

  desc "compile Processing sketches to native applications"
  task :export => ['processing:configure', 'processing:link_libs', 'lib:install', sketches_build_dir, bin_dir] do
    compile = %W{
      #{processing_cmd}
      --export
      --platform=linux
      --bits=32
      --force
    }

    FileList["#{SKETCH_DIR}/*"].each do |sketch|
      sketch_build_dir = "#{sketches_build_dir}/#{File.basename(sketch)}"

      command = "#{compile.join(' ')} --sketch=#{sketch} --output=#{sketch_build_dir}"
      puts command
      puts `#{command}`

      sketch_bin = "#{sketch_build_dir}/#{File.basename(sketch)}"
      fail "Processing export failed to create executable sketch: #{sketch_bin}" unless File.executable? sketch_bin
      cp sketch_bin, bin_dir
    end
  end

end


# TODO: Build sinatra app(?)


# TODO: Deploy via rsync.



### MISC TASKS ###

task :default => ['lib:release', 'sketch:export']
