require 'sinatra/base'
require 'socket'

class WonderdomeControlServer < Sinatra::Base
  # FIXME: Get the port from environment config, or some other more robust mechanism.
  set :bind, ARGV.shift || "localhost"

  get '/send/:message' do
    socket = UDPSocket.new
    socket.send(params[:message], 0, "localhost", 50000)
    socket.close()
    "Message: #{params[:message]}"
  end

  run! if app_file == $0
end
