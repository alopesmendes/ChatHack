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
Le serveur est codé dans la classe **ServerChatHack**, le ServerSocketChannel est à l'état OP_ACCEPT pour pouvoir accepter les clients qui se connectent. Il possède aussi un SocketChannel en OP_CONNECT pour se connecter au serveur MDP. 
Lors d'une connexion, le serveur envoie une demande de vérification du login au serveur base de données et attend la réponse en état OP_READ. En cas de validation, on enregistre le client dans une map<String,Context> avec en clé son login et en valeur son context et on renvoie un ACK pour la connexion au client initial.
Le client lié à la classe  **ChatChaton** se connecte en précisant son adresse, le port d'écoute et son login. Il est tout d'abord en état OP_Connect puis en OP_READ en attendant la réponse du serveur. En cas de refus, on ferme la socketchannel.

# In progress
Nous n'avons pas encore implémenté totalement la connexion privée, en effet les étapes de la connexion qui implique le serveur sont présentes mais pas la connexion elle-même entre les 2 clients.

# Diagramme UML Simplifié

![UML](https://www.plantuml.com/plantuml/img/fLTHR-8u47w-_We_SbgQ7t2gk4r3sgkkBLL0U_UsCia0KOFjivrsgrx_-uxja3Z4qD950fnlPhuPUyRE-AlILEfwMvsT2Pe_qZMGbOng0Tk8WOvqBYB6sRBY-MF9rkJrZE0dhwXIP0xo2MIoeVelj6n4vlFbYPS5gMZDyiteTmk_sL_Fs9YPBsVl6RuTZam2BK0s1C3gBNcGF0UbvfhgMddCqzbzEixIgvlRst6QPRDiFwgIQP0hcWF9BEd5Ok87rkSLxga9qIeio1gq3zYfI50jEbY2ELIGwvd8UG6Dq-PlIZMTzFo_OffqcR_v2YJ7-MiONRvekAnNAro9vNBWgn_vkIpN6tq-QMPfDToGd1VycPsZg9_Ne4gNFIiUpeH4HZNwl-V8_Z2kWLImqeIlGeP_a7_AgYBAIeZU0B4XaoRNcXCgHFKITMHEZcnzXd8hAjW2vfLiQrtYGyEXZfNKIj8jD2lVrSILXzfy-6jk_xpKxtBrnLcvBDdnTSMNj1gPxBaXAN3uASkaAd5g7xPFfUJIcjlHX-qIpXZceUJC6dUF9_hl3EyAiKVbwWtnKzdGnDaU8in0SARWmxmFidp2SSUKmNyrA0s5zN1O_2iUR8Y7-7yn_ZZFGHmCVoVr-8CdeZ7sYh_rFrqqpet20dxeqLnByvRGE0x4ExsUUmQkZfi4ul742YgBcG19JMo4sqDXJ9QuqmYkiHYJXbqeg0lUFcxnTCET7FIN3FovkZt9tn47gSVljjWxnD9LJ898Y2_n0zxVREznvqR_1y9jv1LVb_Wq4qQPLXSEdX21hvzdBejW2FExO0pjfZhf831qsN6-tGwmSzXDgE8OVXaCy271RUTH7jcqfsJPhbNONtpCMxl5JdOsDbkC22vrYojiiq371VODgba4pH_XG5ouSiVNmIti9m9h14WNzfmSyzkCFkDtVyQ3dWcFdDSXYETA24r2Q1e2NKqT48Ym80x3GQT84uB3ocO7Xk4-YIUqf_-tKfMQIxzDkLWGF8g15Ogubh6d-Bm1DaeCmSMVhHxslVf5O9Aki0itMKEPmXP9aiMOT6gMQ49MjLURknSIhLodWgTT3YNkya4oRQy-h8ykycHsVv_-UpEx7xVGzUtiChxzVfVEv_5rEYQju26x-HOlqk-TpL2siphxpKVnEc28B3PDVNiBnSdVks7mpc3d64MJG1lhv0kKcXxOA3e4AtK09GCe7I2kG0De08i3sD27g-nzDA1eoZ603JtRrR1jVx_EhDILHcHxzP1yM0ojRkkXLxp7hYjXHU_IWXU0hrZ6UEtz2G00)
