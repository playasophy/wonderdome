require 'sinatra'
require 'socket'

get '/send/:message' do
  socket = UDPSocket.new
  socket.send(params[:message], 0, "localhost", 50000)
  "Message: #{params[:message]}"
  socket.close()
end
