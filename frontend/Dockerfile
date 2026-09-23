# Use the Node.js version pinned by mise.toml (Angular requires Node 22/24/26).
FROM node:24

# Set the working directory inside the container
WORKDIR /usr/src/app

# Copy package.json and package-lock.json to the container
COPY package*.json ./

COPY angular.json ./

COPY tsconfig.json ./

COPY ngsw-config.json ./

COPY getBuildDate.js ./

# Copy the rest of the application code to the container
COPY ./src ./src

# Install project dependencies
RUN npm install --force

# Expose the port that your Angular app will run on (default is 4200)
EXPOSE 4200

# Start the Angular app with live reload when the container starts
CMD ["npm", "run", "next-docker"]
