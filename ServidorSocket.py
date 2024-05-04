import socket
import threading
import time

client_dict = {}

def handle_client(client_socket, addr):
    client_id = f"{addr[0]}:{addr[1]}"
    print(f"Nueva conexión de {client_id}")
    
    while True:
        try:
            data = client_socket.recv(1024)
            if not data:
                break
            print(f"Recibido de {client_id}: {data.decode()}")
        except ConnectionResetError:
            print(f"Conexión perdida con {client_id}")
            break

    print(f"Cerrando conexión con {client_id}")
    client_socket.close()
    client_dict.pop(client_id, None)

def main():
    server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_socket.bind(('0.0.0.0', 5000))
    server_socket.listen(5)
    print("Servidor escuchando en puerto 5000")

    try:
        while True:
            client_socket, addr = server_socket.accept()
            client_id = f"{addr[0]}:{addr[1]}"
            client_dict[client_id] = client_socket

            client_thread = threading.Thread(target=handle_client, args=(client_socket, addr))
            client_thread.start()
    except KeyboardInterrupt:
        print("Servidor interrumpido")
    finally:
        print("Cerrando servidor...")
        for client in client_dict.values():
            client.close()
        server_socket.close()

if __name__ == "__main__":
    main()



