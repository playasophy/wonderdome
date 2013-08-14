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

# hash of command paths
COMMANDS = {} # TODO

# processing paths
SKETCHBOOK_PATH = ENV['SKETCHBOOK_HOME'] || "#{ENV['HOME']}/sketchbook"
processing_home = ENV['PROCESSING_HOME'] || "#{ENV['HOME']}/processing"
processing_cmd = nil

# deployment config
SSH_USER    = "wonder@wonderdome"
SSH_PORT    = "22"
DEPLOY_ROOT = "~"



### UTILITY METHODS ###

# Add colorization methods to strings.
class String
  def bold;    "\033[1m#{self}\033[22m" end
  def black;   "\033[30m#{self}\033[0m" end
  def red;     "\033[31m#{self}\033[0m" end
  def green;   "\033[32m#{self}\033[0m" end
  def yellow;  "\033[33m#{self}\033[0m" end
  def blue;    "\033[34m#{self}\033[0m" end
  def magenta; "\033[35m#{self}\033[0m" end
  def cyan;    "\033[36m#{self}\033[0m" end
  def white;   "\033[37m#{self}\033[0m" end
end


# Colorize FileUtils command output yellow.
@fileutils_output = Module.new do
  def self.puts(message)
    STDOUT.puts(message.to_s.yellow)
  end
end


# Present a banner identifying a task.
def banner(name)
  puts "%1$s %2$s" % ['>>>'.cyan, name.bold.white]
end


# Fails the build by printing a red message.
def fail(message)
  raise message.red
end


# Prints a test result message.
def puts_result(type, message="")
  status = case type
           when :ok   then " OK ".green
           when :pass then "PASS".green
           when :fail then "FAIL".red
           else type.to_s.yellow
           end

  puts "  [%4s] %s" % [status, message]
end


# Locates a command and ensures it is executable.
def locate_command(name, dir=nil, msg="")
  command = nil

  if dir && File.directory?(dir)
    command = "#{dir}/#{name}"
  else
    path = `which #{name}`
    command = path.chomp if $?.success?
  end

  command = File.readlink(command) while command && File.symlink?(command)

  raise "Unable to locate '#{name}' command! #{msg}" if command.nil?
  raise "Command does not exist: #{command} #{msg}" unless File.exist? command
  raise "Command is not executable: #{command} #{msg}" unless File.executable? command

  command
end


# Executes a command and prints the results.
def execute(*commands)
  command = commands.flatten.join(' ')
  @fileutils_output.puts command
  system(command)
end


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

  execute 'rsync', opts, src, dest
end




### BUILD TASKS ###

# 'clobber' should remove all generated files
CLOBBER << BUILD_DIR


namespace :processing do

  sketchbook_lib_dir = "#{SKETCHBOOK_PATH}/libraries"

  # processing directories
  directory sketchbook_lib_dir

  # desc "Locate Processing directory and compiler."
  task :configure do
    banner "Locating Processing"

    begin
      processing_cmd = locate_command 'processing-java', processing_home
      processing_home = File.dirname(processing_cmd)
      puts_result :ok, processing_home
    rescue => e
      puts_result :fail, e.message
      fail "Install Processing in your home directory or add it to your $PATH."
    end
  end

  desc "Check for required Processing libraries."
  task :check_libs do
    banner "Checking library dependencies"

    missing = false
    ['PixelPusher', 'udp'].each do |lib|
      if File.directory? "#{LIB_DIR}/#{lib}"
        puts_result :ok, lib
      else
        puts_result :fail, lib
        missing = true
      end
    end

    fail "Missing required Processing libraries. Install them locally in '#{LIB_DIR}'." if missing
  end

  desc "Link Processing libraries in user's sketchbook."
  task :link_libs => [:check_libs, sketchbook_lib_dir] do
    banner "Installing Processing libraries"

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

  classpath = []
  commands = {}

  # library directories
  directory classes_dir
  directory lib_dir
  directory lib_src_dir
  directory lib_doc_dir
  directory lib_lib_dir

  CLEAN << classes_dir

  # desc "Checks that there is an installed JDK with the necessary commands."
  task :jdk do
    banner "Locating JDK commands"

    java_bins = "#{ENV['JAVA_HOME']}/bin" if ENV['JAVA_HOME']
    missing = false

    [:javac, :jar, :javadoc].each do |command|
      begin
        commands[command] = locate_command command.to_s, java_bins
        puts_result :ok, commands[command]
      rescue => e
        puts_result :fail, e.message
        missing = true
      end
    end

    fail "Unable to locate required JDK commands. Install a JDK and set $JAVA_HOME or add the commands to your $PATH." if missing
  end

  # desc "Determines the classpath for the library."
  task :classpath => ['processing:configure', 'processing:check_libs'] do
    banner "Calculating library classpath"

    classpath << "#{processing_home}/core/library/core.jar"
    classpath << FileList["#{LIB_DIR}/**/*.jar"]
    classpath.flatten!

    puts "CLASSPATH: #{classpath.join(':')}"
  end

  desc "Compile library source files."
  javac :compile => [:jdk, :classpath, classes_dir] do |t|
    banner "Compiling library sources"

    t.classpath = classpath
    t.src << Sources[SRC_DIR, "**/*.java"]
    t.src_ver  = '1.6'
    t.dest = classes_dir
    t.dest_ver = '1.6'
  end

  jar lib_jar_file => [:jdk, :compile, lib_lib_dir] do |t|
    banner "Building library JAR"

    t.files << JarFiles[classes_dir, "**/*.class"]
  end

  # desc "Build library jar file."
  task :jar => lib_jar_file

  # desc "Copy library sources to output."
  task :copy_src => lib_src_dir do
    banner "Copying library sources"

    rsync "#{SRC_DIR}/", "#{lib_src_dir}/", exclude: 'library.properties'
  end

  desc "Generate library documentation."
  task :doc => [:jdk, :classpath, lib_doc_dir] do
    banner "Generating library documentation"

    execute %W{
      #{commands[:javadoc]}
      -classpath #{classpath.join(':')}
      -sourcepath #{SRC_DIR}
      -subpackages org.playasophy.wonderdome
      -doctitle WonderDome
      -d #{lib_doc_dir}
    }
  end

  desc "Build all library components."
  task :build => [:jar, :copy_src, :doc] do
    banner "Building Library"

    cp "#{SRC_DIR}/library.properties", lib_dir
  end

  desc "Construct a zip of the library for distribution."
  task :release => :build do
    banner "Packaging library release"

    `cd #{File.dirname(lib_dir)} && zip -r #{LIBRARY_NAME}.zip #{File.basename(lib_dir)}`
  end

  desc "Link to the compiled library in the user's sketchbook."
  task :install => :build do
    banner "Installing library"

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

  # common requirements for invoking Processing
  task :prereqs => ['processing:configure', 'processing:link_libs', 'lib:install', sketches_build_dir]

  desc "Run a Processing sketch."
  task :run => :prereqs do |t, args|
    fail "NYI: run a sketch with args: #{args.inspect}" # TODO
  end

  desc "Compile Processing sketches to native applications."
  task :export => [:prereqs, bin_dir] do
    banner "Exporting Processing sketches"

    FileList["#{SKETCH_DIR}/*"].each do |sketch|
      sketch_build_dir = "#{sketches_build_dir}/#{File.basename(sketch)}"

      execute %W{
        #{processing_cmd}
        --sketch=#{sketch}
        --output=#{sketch_build_dir}
        --export
        --platform=linux
        --bits=32
        --force
      }

      sketch_bin = "#{sketch_build_dir}/#{File.basename(sketch)}"
      fail "Processing export failed to create executable sketch: #{sketch_bin}" unless File.executable? sketch_bin
      cp sketch_bin, bin_dir
    end
  end

end


namespace :web do

  desc "Build the Wonderdome controller app."
  task :build do
    banner "Building controller app"

    puts "NYI: build the webapp" # TODO
  end

  desc "Run the controller app in a webserver."
  task :run do
    banner "Running controller webserver"

    raise "NYI: run the webserver" # FIXME
  end

end


namespace :deploy do

  # TODO: Deploy via rsync.
  # TODO: restart the running webserver

end


task :default => ['lib:release', 'sketch:export', 'web:build']
