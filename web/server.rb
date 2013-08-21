require 'sinatra/base'
require 'socket'

# Constants
HEALTH_CHECK_PERIOD = 1
WONDER_PROCESSOR_COMMAND = "wonder_processor"

$process_id = nil
def start_process
  puts "Spawning new process with command line '#{WONDER_PROCESSOR_COMMAND}'"
  $process_id = Process.spawn(WONDER_PROCESSOR_COMMAND, :pgroup=>true)
  puts "Process spawned with pid #{$process_id}"
end

def kill_process
  puts "Issuing kill signal to process group #{$process_id}"
  Process.kill -9, $process_id
  puts "Kill signal issued, waiting for process group #{$process_id}"
  Process.wait -$process_id
  puts "Process reaped successfully"
end

def restart_process
  $run_health_check = false
  kill_process
  start_process
  $run_health_check = true
  "Processing restarted successfully"
end

def terminate
  puts "Stopping the health check thread"
  $run_health_check = false
  kill_process
  # Start a thread which will exit after a brief delay, to allow the server
  # to send the response.
  puts "Starting delayed exit thread"
  Thread.new do
    sleep 0.2
    puts "Exiting"
    exit!
  end
  "Terminating web server"
end

# Start the wonderdome process.
start_process

# Start a thread to check on the status of the wonderdome process.
# If it has quit, restart it.
$run_health_check = true
Thread.new do
  while true do
    sleep HEALTH_CHECK_PERIOD
    continue unless $run_health_check
    reaped = Process.wait -$process_id, Process::WNOHANG
    if !reaped.nil?
      puts "Reaped process #{reaped} with status #{$?.inspect}; restarting..."
      start_process
    end
  end
end

$socket = UDPSocket.new
def send_message(*message)
  $socket.send(message.join("|"), 0, "localhost", 50000)
end

class WonderdomeControlServer < Sinatra::Base

  # FIXME: Get the port from environment config, or some other more robust mechanism.
  set :bind, ARGV.shift || "0.0.0.0"

  get '/' do
    erb :control
  end

  get '/about' do
    erb :about
  end

  get '/control' do
    erb :control
  end

  post '/control' do
    puts params.inspect
    send_message("control", params[:type], params[:button])
  end

  get '/admin' do
    erb :admin
  end

  post '/admin' do
    puts params.inspect
    if params[:action] == "restart"
      restart_process
    elsif params[:action] == "terminate"
      terminate
    else
      send_message("admin", params[:action])
    end
  end

  get '/send/:message' do
    send_message("raw_message", params[:message])
    "Message: #{params[:message]}"
  end

  run! if app_file == $0

end
