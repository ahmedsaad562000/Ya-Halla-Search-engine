# Search Engine Project




[![Watch the video](https://img.youtube.com/vi/)](https://github.com/ahmedsaad562000/Ya-Halla-Search-engine/assets/76961547/049b9923-df2d-4555-a4a7-497569dc0b8e)

## Description
<strong>Ya Halla</strong> is a search engine that is built using Java. The project is divided into two parts: the back-end and the front-end. The back-end is built using Java and Spring Boot. The front-end is built using ReactJS. The back-end is responsible for crawling the web, indexing the crawled pages, and providing the front-end with the search results. The front-end is responsible for providing the user with a search bar and displaying the search results.




## How to run the Project


### Connect your database
- You can place your <strong>MongoDB</strong> connection-string in the <strong>DB_Controller</strong> class placed in <strong>DBController</strong> Package  

### Running The WebCrawler
- The <strong>Web Crawler</strong> takes two CLI arguments ( number of links to generate , number of threads )
- you can use ``javac MainCrawler.java`` to compile via terminal
- you can use ``java MainCrawler.java A B`` to run via terminal , where ``A`` is number of Generated Links  and  ``B`` is the number of threads
- or Just Run as a normal java file after adding CLI arguments from Configurations in your IDE (intellij , Eclipse , etc......)
### Running The Indexer
- you can use ``javac Indexer_threading.java`` to compile via terminal
- you can use ``java Indexer_threading.java`` to run via terminal 
- or Just Run as a normal java file in your IDE (intellij , Eclipse , etc......)
### Starting the back-end

- Start the Spring Application located in <strong>"Backend"</strong> folder
- you can use ``mvn spring-boot:run`` to run via terminal , port is 8083
- or Just Run as a normal java file from your IDE (intellij , Eclipse , etc......)

### Starting the front-end

- Open a terminal in the <strong>"Frontend"</strong> folder
- Execute the command ```npm install``` to install the dependencies
- Execute the command ```npm start```
- Open ``localhost:3000`` in your browser



