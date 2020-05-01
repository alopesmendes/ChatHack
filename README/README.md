# <center> Tracking object
### Authors: LOPES MENDES Ailton, LAMBERT--DELAVAQUERIE Fabien
## Usage

### Linux
```bash
#First you should go to Jars
cd Jars
#then execute the jars in the following order
java -jar ServerMdp.jar port file.txt
java -jar ServerChatHack.jar port file.txt
java -jar ChatHack.jar directory host port login [password]
# If you want to generate the jars.
# go to the reportory where there's the build.xml
cd ChatHack
# then execute the build.xml with ant
ant -f build.xml
#generates the jars if you want to generate the javadoc you can enter ant doc
# You should go the reportory where the jars are
cd build/jar
java -jar ServerChatHack.jar port file.txt
java -jar ChatHack.jar directory host port login [password]
```

