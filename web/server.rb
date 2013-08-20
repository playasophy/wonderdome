require 'sinatra/base'
require 'socket'

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
