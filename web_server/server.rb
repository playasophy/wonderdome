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

  post '/control/up' do
    send_message("control", "up")
    redirect '/control'
  end

  post '/control/down' do
    send_message("control", "down")
    redirect '/control'
  end

  post '/control/left' do
    send_message("control", "left")
    redirect '/control'
  end

  post '/control/right' do
    send_message("control", "right")
    redirect '/control'
  end

  post '/control/a' do
    send_message("control", "a")
    redirect '/control'
  end

  post '/control/b' do
    send_message("control", "b")
    redirect '/control'
  end

  post '/control/select' do
    send_message("control", "select")
    redirect '/control'
  end

  post '/control/start' do
    send_message("control", "start")
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
