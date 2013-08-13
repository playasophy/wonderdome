require 'sinatra/base'
require 'socket'

class WonderdomeControlServer < Sinatra::Base
  # FIXME: Don't hardcode port to bind to.
  set :bind, '192.168.4.244'

  get '/send/:message' do
    socket = UDPSocket.new
    socket.send(params[:message], 0, "localhost", 50000)
    socket.close()
    "Message: #{params[:message]}"
  end

  run! if app_file == $0
end
