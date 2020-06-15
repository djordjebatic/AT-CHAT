# AT-CHAT

## Start up

To run:
* start a wildfly 11 server (standalone-full) and change public interface to your ip adress. Access the frontend on http://{ip-address}:8080/WAR2020
* to start other hosts, set master ip adress with port ({ip}:{port}) in src/server/connections.properties 

## Implementacija/Objasnjenje

Inicijalizacija cvora zapocinje tako sto se dobavi adresa master cvora iz config fajla ukoliko nije prazna. Nakon toga salje se rest poziv koji registrue novi cvor, a master se dodaje u listu cvorova na lokalnom cvoru. Zatim se setuju registrovani i logovani korisnici.

Prilikom svakog logovanja ili registracije salje se zavtev ostalim cvorovima u kojima se setuju korisnici. Poruke se prosledjuju queue sesiji koja ukoliko nema primalaca salje poruku u soketu, a ukoliko je primalac drugi cvor salje se rest zavtev sa inputom u formi Message (JSON) objekta.
