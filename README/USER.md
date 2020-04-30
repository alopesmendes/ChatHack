# <center> Introduction
Ce projet a deux exécutables le ***ServerChatHack.jar*** et ***ChatHack.jar*** pour les exécuter il faut d'abord exécuter le ***ServerMdp.jar*** il prend en paramétres un port et un fichier avec des logins et mots de passe au format *login$password*. Ensuite il faudra exécuté le ServerChatHack.jar il prend en paramétres un port puis le host et port du ServerMdp. Finalement on pourra exécuter les ChatHack, il prend en paramétres un dossier le host et port du ServerChatHack puis un login et possiblement un mot de passe.

# Usage
- Point de départ:
```java
// First terminal
java -jar ServerMdp.jar 4545 password.txt

// Second terminal
java -jar ServerChatHack.jar 7777 localhost 4545

// Third terminal
java -jar ChatHack.jar /home/Documents localhost 7777 bruno

// ou bob et alice qui sont dans password.txt
java -jar ChatHack.jar /home/Documents localhost 7777 bob alice 
```

- Les commandes du ChatHack disponible

|Commande | Résultat |
|--|--|
|[texte]| message global |
|/login ou @login| demande de connexion privée si la connexion n'est pas encore établie|
|@login [texte]| (demande de connexion privée) message privée |
|/login [fichier]| (demande de connexion privée) fichier privée |
|{^Z ou ^z} | demande de déconnexion du serveur |
|{^Z ou ^z} login| demande de déconnexion privée |

- Des exemples d'usage

|Type commande | Ecriture de la commande à partir du 1er client  | 1er Client bob | 2eme Client alice |
|--|--|--|--|
| Message global | Salut tout le monde | bob:Salut tout le monde | bob:Salut tout le monde |
| Demande de connexion privée | @alice salut ou @alice text.txt ou @alice ou /alice | 1->@alice ou /alice<br><br>3->INFO: alice accepted/denied the demand | INFO: bob wants to start a private conversation enter O/N <br><br>2->O ou o// n ou N<br><br>4-><br>*Si la demande est accepte*<br>INFO: Client bob with the token [long]
| Message privée|@alice Salut|alice read the message|private message from bob : Salut|
| Fichier privée|/alice text.txt|alice received the file|INFO: Received file text.txt from bob|
| Déconnexion privée|^z alice|INFO: Logout to client alice|INFO: Logout to bob|
| Déconnexion |^z |Logout to server: [host/port] | - |

# Règles
Tout d'abord faut s'identifier avec un login unique et si le login appartient à password.txt il faudra aussi rentrer le mot de passe sinon il faut juste mettre un login.

    INFO: Connection failed
   
   Si le serveur Mdp se déconnecte on pourra plus accepter de nouveaux clients même si un client était déjà connecte et essayer de se reconnecte.
   
  Si on demande de la connexion privée d'un client inexistant ou le client refuse la demande.
  
    INFO: failed private connexion demand
    
 Si on demande de se déconnecte ou se connecte à nous même.
 

    INFO: Cannot logout with yourself
    ou
    INFO: Cannot create private connexion with yourself
Si on essaye d'envoyer un fichier qui n'existe pas dans notre répertoire

    INFO: Cannot send this file does not exist:[file]

 Pour les demandes de connexion privées le client a 5000 millisecondes pour envoi une réponse sinon par défaut la demande est refusée.
 

    //client qui a reçu la demande
    INFO: more than 5000 milleseconds have passed you did not respond
    //client qui a fait la demande
    INFO: [login] denied the demand

  Finalement si la connexion entre le client et le serveur est ferme
  

    SEVERE: server closed it's connexion with you

   

# Conclusion 
Le client peut communiquer avec des autres client en général ou en privée. Le rôle du serveur sera de transmettre des messages mais aussi les demandes. Le serveur mdp reste important pour établir la connexion entre notre serveur et ces client.




