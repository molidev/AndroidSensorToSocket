import socket

import sys
import signal

def sigint_handler(sig, frame):
    print("Se ha recibido una señal SIGINT (Ctrl+C). Cerrando el servidor...")
    sys.exit(0)

def main():
    host = '0.0.0.0'
    port = 5001         

    # Socket UDP/IP
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    server_socket.bind((host, port))

    print("Servidor escuchando en el puerto", port)

    while True:
        data, client_address = server_socket.recvfrom(1024)
        print("Mensaje recibido de", client_address, ":", data.decode())

if __name__ == "__main__":
    main()
