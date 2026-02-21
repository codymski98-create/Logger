const CACHE_VERSION = 'v1';
const CACHE_NAME = `logger-${CACHE_VERSION}`;
const urlsToCache = ['/', '/index.html', '/manifest.json', 'https://unpkg.com/leaflet/dist/leaflet.js', 'https://unpkg.com/leaflet/dist/leaflet.css'];

self.addEventListener('install', event => {
    console.log('Service Worker installing...');
    event.waitUntil(caches.open(CACHE_NAME).then(cache => {
        console.log('Caching app shell');
        return cache.addAll(urlsToCache).catch(err => {
            console.warn('Some resources failed to cache:', err);
            return Promise.resolve();
        });
    }).then(() => self.skipWaiting()));
});

self.addEventListener('activate', event => {
    console.log('Service Worker activating...');
    event.waitUntil(caches.keys().then(cacheNames => {
        return Promise.all(cacheNames.map(cacheName => {
            if (cacheName !== CACHE_NAME) {
                console.log('Deleting old cache:', cacheName);
                return caches.delete(cacheName);
            }
        }));
    }).then(() => self.clients.claim()));
});

self.addEventListener('fetch', event => {
    const { request } = event;
    const url = new URL(request.url);
    if (request.method !== 'GET') {
        return;
    }
    if (url.protocol === 'chrome-extension:') {
        return;
    }
    event.respondWith(fetch(request).then(response => {
        if (!response || response.status !== 200 || response.type === 'error') {
            return response;
        }
        const responseToCache = response.clone();
        caches.open(CACHE_NAME).then(cache => {
            cache.put(request, responseToCache);
        });
        return response;
    }).catch(() => {
        return caches.match(request).then(response => response || new Response('Offline - Content not available', { status: 503 }));
    }));
});
