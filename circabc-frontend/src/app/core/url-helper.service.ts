import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, Subscriber } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class UrlHelperService {
  private cacheUrls: Map<string, string> = new Map();
  constructor(private http: HttpClient) {}

  get(url: string): Observable<string> {
    return new Observable((observer: Subscriber<string>) => {
      let objectUrl: string | null = null;
      if (this.cacheUrls.has(url)) {
        objectUrl = this.cacheUrls.get(url) ?? null;
        observer.next(objectUrl ?? undefined);
      } else {
        this.http.get(url, { responseType: 'blob' }).subscribe((m) => {
          objectUrl = URL.createObjectURL(m);
          this.cacheUrls.set(url, objectUrl);
          observer.next(objectUrl);
        });
      }
      const cacheSize = 64;
      if (this.cacheUrls.size > cacheSize) {
        return () => {
          if (objectUrl) {
            URL.revokeObjectURL(objectUrl);
            this.cacheUrls.delete(url);
            objectUrl = null;
          }
        };
      } else {
        return;
      }
    });
  }
}
