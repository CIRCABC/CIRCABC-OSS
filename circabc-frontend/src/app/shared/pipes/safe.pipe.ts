import { Pipe, PipeTransform } from '@angular/core';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

export function isAllowedResourceUrl(url: string | undefined): url is string {
  if (!url) {
    return false;
  }

  try {
    const parsedUrl = new URL(url);
    return parsedUrl.protocol === 'http:' || parsedUrl.protocol === 'https:';
  } catch {
    return false;
  }
}

@Pipe({
  name: 'cbcSafe',
})
export class SafePipe implements PipeTransform {
  constructor(private sanitizer: DomSanitizer) {}
  transform(url: string | undefined): SafeResourceUrl {
    if (isAllowedResourceUrl(url)) {
      return this.sanitizer.bypassSecurityTrustResourceUrl(url);
    }
    throw new Error('Only http and https resource URLs are allowed');
  }
}
