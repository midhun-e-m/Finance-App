# === STAGE 1: Build ===
# Use Node.js to install dependencies and build the React app
FROM node:20-alpine AS build
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm install

# Copy source code and build for production
COPY . .
# Set the API URL for the build process (it will point to our Docker backend)
ENV VITE_API_URL=http://localhost:8080 
RUN npm run build

# === STAGE 2: Serve ===
# Use Nginx, a blazing fast web server, to serve the static HTML/JS files
FROM nginx:alpine

# Copy the compiled 'dist' folder from the build stage to Nginx's public folder
COPY --from=build /app/dist /usr/share/nginx/html

# Expose port 80 (Standard HTTP)
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]