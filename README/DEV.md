# Architecture du code CHATHACK
# Packages
## Frame
Le package **Frame** possède une classe static dans l'interface Data pour chaque trame correspondant au RFC du protocole. Cela nos permet de différencier les informations de chaque type de trames utilisées.
On utilise une classe Enum **StandardOperation** pour regrouper les Code_OP définis aux actions des trames.
L'interface Frame permet à partir d'une classe de Data de créer une objet Frame via une factory ainsi qu'une méthode buffer() qui renvoie un ByteBuffer correspondant à la trame. 
On retrouve donc une classe qui implémente **Frame** pour chaque trame nécessaire à notre protocole.
On utilise un Visitor avec la classe **FrameVisitor**, il en existe une instance différente pour le serveur et chaque client.
## Reader
Le package **Reader** est composé essentiellement de l'interface **Reader** qui permet à partir d'un ByteBuffer de récupérer un objet Data (au final) qui correspond aux informations de la trame liée au ByteBuffer. 
Nous utilisons une classe **SelectOpCodeReader** qui permet la sélection du Reader à utilisé via l'Op_Code contenu dans le ByteBuffer.
**Reader** est une interface paramétrée, nous avons divisé les classes qui l'implémentent dans deux "sous-packages": 
- "**basics**" qui regroupe les Readers de types simples qui renvoie des objets du type correspondant
- "**data**" qui utilisent les Readers basiques pour renvoyer des objets Data.
## NonBlocking
Ce package correspond au serveur et au client.
Le serveur est codé dans la classe **ServerChatHack**, le ServerSocketChannel est à l'état OP_ACCEPT pour pouvoir accepter les clients qui se connectent. Il possède aussi un SocketChannel en OP_CONNECT pour se connecter au serveur MDP.  Nous avons deux factory dans la classe **ServerChatHack** qui va crée des visiteurs différents. Il aura un qui sera utilise pour la communication client serveur et un autre pour la communication serveur, cela évite que le client puisse communique s'il n'est pas encore connecte.
Lors d'une connexion, le serveur envoie une demande de vérification du login au serveur base de données et attend la réponse en état OP_READ. On garde la réponse dans une map<Long, DataMdp>, la classe DataMdp va nous permettre de garder la demande du client et la réponse du serveur.En cas de validation, on enregistre le client dans une map<String, SelectionKey> avec en clé son login et en valeur sa SelectionKey et on renvoie un ACK pour la connexion au client initial. 
Le client lié à la classe  **ChatChaton** se connecte en précisant son adresse, le port d'écoute et son login. Il est tout d'abord en état OP_Connect puis en OP_READ en attendant la réponse du serveur. En cas de refus, on ferme la socketchannel.
Suite à la connexion d'un client on envoyer des messages public, c'est à dire qu'on envoyé une trame global au serveur qui va la transmettre à tous les clients connectes au serveur.
Le client pourra envoyer des demandes de connections privées et ça sera au serveur de relier les demandes et les réponses entre les client (expliquer plus en détail dans le protocole). Si les clients décide d'établir une connexion ça sera aux clients de se connecter à un autre et la communication privée c'est chaque client qui s'en occupent d'envoyer les trames et comment les lires, le serveur n'intervient pas.

# Diagramme UML Simplifié

![UML](https://www.plantuml.com/plantuml/img/hLVTSjew4BukJw5BpeJu0JePJWaXQQO4Ci3qdBkEiDUWGLW-iapAzEJTkvBy8zlOWIRC0DAkzji_UTVx9L5KgdJFUxsO-Zkw0H9ABurXxqMWFEN7NYIYDHV-ZaKRygj7yEDpcYHa2V80ShobwYjAPYpzkJe85X1EqyZVzl-gaMF98lKDZibfpX8u-0e2FD6tQoOYt90T7At0g_brxD0MwA-8kcruxRssEoc11Y0p08ZIFNcMmeSaMIggqiH1lflF9eF5vFvnEXrC5elvecuLUW4of3wGXG6zMGrTmwguiTsvmLYH3QX6T2Ga1RKH0XkfUUoB03AbsTyTLNHOqVyBCwwO_-eU83xwhw1_UrHmcuOXHcAzRkYgMdujsMQhheUPb-Q4NPBhGBn4ryYgNf6JHyhi6NRJ4-Bfezx_5KNc9n8A28TG4H6U4lnC_c6SayHmYDe2CIQJBDTA41h7_EYLO9QFQ5K4jeyvx07pIlQfOhZ9C9AkAnLAkeSiykMTk7Ue9XzkpCsVa_eyLrTMocR9h1-uM5FUrzcpop5dQ209a72E_4HA8OsuMPqjDnPHP1zA8rnkBzHV2Zu5SGtAtXgaNug68bQs3N01iOWIE1ltMR83haka1VoNGeBroMXePxz7Wp6n3V-Tzezy7-9MytFkfVXFM1Nm2N11CzAbIFUCLs3q_coCaRypilX_gTvSjDoUZJ0L6v4g8ssNZkJ9hg1FEWMWGDRq5VnK6NY-xI-LrArNuVg4FOqAK4gxP4IXKK1bC8z1Kfri8aoj790CduiDYuhTtkQlHQ6j6QKcMqswLDGvqZufOsItw6YKbb9TdTMXUOtLbVZw_4yarvsTn_emvJUMF2GnTjCN8OCtRIfhr2cp2kv5HVADejMfA2yRnj_SUIxGLTRcJ4VrAbLrLC0tWevduYBU8PEOREDzGKi0yn9Ouhac5XMgmyAYndsaWGFz09DOI8UDxyIWHW6-cILPh9NOGNH-kEjvRMq67-j99eEQkatDQtZ3mBpUjvhr1oN5BVWDXQOjN5Z1GfIPONqU4ESDInFjiiKCNVbBzRejV6nLSnfCmyjtQAl6e4LtrfvAlLdhEY-mZOvM7q9CD_pE4gQ4T5zmRrO4MnT4GKBii54v-BA5g3_M03T_5-TmObB760sun_bDMuIy15-k9LaDI7dC00r9c3eUvmsQSljsZ57oWHtoX3lscoclwNjf-NW-cqt-VPpF1WNfOJg_7Kr_F4sMoz73P40AnlFYyVjeDVbHoZHvkLJlaql5GL836ThTn9KtfD7uMxuqqsOcRN-ld7AFOyEdIigTvbRpSQWx8eP4zRFsc5zN9pecTfutF35OENmpcbKpQhWs2PwPfqxncs4lpchW4YE_9bqZvUc3jS6o7FAKYFM8PnpuJ9veTCJQ7h032rBAJMzEF3tNVS4xbkxvRm00)

