require 'sinatra/base'
require 'socket'

$socket = UDPSocket.new

def send_message(*message)
  $socket.send(message.join("|"), 0, "localhost", 50000)
end

class WonderdomeControlServer < Sinatra::Base

  # FIXME: Get the port from environment config, or some other more robust mechanism.
  set :bind, ARGV.shift || "localhost"

  get '/control' do
    erb :control
  end

  post '/control/A' do
    send_message("control", "A")
    redirect '/control'
  end

  post '/control/B' do
    send_message("control", "B")
    redirect '/control'
  end

  get '/admin' do
    erb :admin
  end

  post '/admin' do
    "Params: #{params.inspect}"
  end

  post '/admin/pause' do
    send_message("admin", "pause")
    redirect '/admin'
  end

  post '/admin/resume' do
    send_message("admin", "resume")
    redirect '/admin'
  end

  get '/send/:message' do
    send_message("raw_message", params[:message])
    "Message: #{params[:message]}"
  end

  run! if app_file == $0

end
