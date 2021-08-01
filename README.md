# Programming 3 code repository

* Miikka Niemi
* 1948190
* email: niemimi@student.oulu.fi

## How to build and run

    build:
    from root where 'pom.xml' is located -> mvn package

    run:
    java -jar .\target\chatserver-1.0-SNAPSHOT-jar-with-dependencies.jar chat.db keystore.jks password

Repository includes default certificate 'keystore.jks' with password 'password'.


    Usage: java -jar chat-server-file.jar <DATABASE> <CERT> <PASSWORD> [debug={LEVEL}]

        DATABASE     database file
        CERT         JKS certificate file
        PASSWORD     certificate password

        Optional:
        debug={LEVEL}    print additional logging information where LEVEL={1|2|3}.
                        Also writes a log file 'server.log' to current dir.
                            LEVEL
                            1    warnings
                            2    informative
                            3    detail, lots of data. Use carefully

    Examples:
        java -jar chat-server-file.jar chat.db keystore.jks password
            Use given database and certificate.

        java -jar chat-server-file.jar chat.db keystore.jks password debug=3
            Write lots of information to 'server.log' file
