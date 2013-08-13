require 'sinatra'
require 'socket'

get '/send/:message' do
  UDPSocket.new.send(params[:message], 0, "localhost", 50000)
  "Message: #{params[:message]}"
end
