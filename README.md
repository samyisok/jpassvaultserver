[![Build(Gradle)](https://github.com/samyisok/jpassvaultserver/actions/workflows/gradle.yml/badge.svg)](https://github.com/samyisok/jpassvaultserver/actions/workflows/gradle.yml)
![Coverage](.github/badges/jacoco.svg)
# Jpassvaultserver for solo user

Online sync service for [jpassvault](https://github.com/samyisok/jpassvault)

# Setup

Build jar. 

    ./greadlew build

Prepare VM with your OS of choice. Ubuntu 20.04 in example.
Create user without shell, and place jar into home dir of this user.

    adduser --disabled-password --disabled-login jpassvaultserver

Create systemd service:

    touch /etc/systemd/system/jpassvaultserver.service
    vim /etc/systemd/system/jpassvaultserver.service

Configure your service, and add secret key in Env params.

    [Unit]
    Description=jpassvaultserver instance
    Wants=network-online.target
    After=network-online.target
    
    [Service]
    User=jpassvaultserver
    Group=jpassvaultserver
    Restart=always
    RestartSec=30
    WorkingDirectory=/home/jpassvaultserver/
    Environment="JPASSVAULT_SECRET=*KEY_HERE*"
    ExecStart=/usr/bin/java -jar jpassvaultserver-1.0.0.jar
    
    [Install]
    WantedBy=multi-user.target


Reload systemd and enable service:

    sudo systemctl daemon-reload
    sudo systemctl enable example.service
    sudo systemctl start example.service

Check service, it should be in running state:

    sudo systemctl status example.service
    
Default port and address is 0.0.0.0:9393 but you can configure it from command line

    java -jar jpassvaultserver-1.0.0.jar --server.port=8080
