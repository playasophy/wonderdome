require 'sinatra/base'
require 'socket'

# Constants
HEALTH_CHECK_PERIOD = 1
WONDER_PROCESSOR_COMMAND = "wonder_processor"

$process_id = nil
def start_process
  $process_id = Process.spawn(WONDER_PROCESSOR_COMMAND, :pgroup=>true)
end

# Start the wonderdome process.
start_process

# Start a thread to check on the status of the wonderdome process.
# If it has quit, restart it.
Thread.new do
  while true do
    sleep HEALTH_CHECK_PERIOD
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
    send_message("control", params[:type], params[:button])
    puts params.inspect
  end

  get '/admin' do
    erb :admin
  end

  post '/admin' do
    send_message("admin", params[:button])
    puts params.inspect
  end

  get '/send/:message' do
    send_message("raw_message", params[:message])
    "Message: #{params[:message]}"
  end

  run! if app_file == $0

end
