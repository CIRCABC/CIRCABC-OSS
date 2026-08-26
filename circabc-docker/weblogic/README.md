# pull an image from the Oracle Container Registry

In a web browser, navigate to https://container-registry.oracle.com and login via the Oracle Single Sign-On authentication service.

Use the web interface to accept the Oracle Standard Terms and Restrictions for the Oracle software images that you intend to deploy. Your acceptance of these terms are stored in a database that links the software images to your Oracle Single Sign-On login credentials. Your acceptance of the Oracle Standard Terms and Restrictions is valid only for 8 hours from the time you last accepted it. This is subject to change without notice. If you have not pulled the image within the valid period for acceptance, you need to repeat the process before you attempt to pull the image.

Use the web interface to browse or search for Oracle software images.

On the host system, use the docker login command to authenticate against the Oracle Container Registry using the same credentials that you used to log into the web interface:

# docker login container-registry.oracle.com
The command prompts you for your username and password.

 docker-compose -f docker-compose-weblogic.yml build

  docker-compose -f docker-compose-weblogic.yml up
