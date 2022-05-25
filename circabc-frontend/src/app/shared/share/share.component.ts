import { Component, Input, OnInit } from '@angular/core';

import { FormBuilder, FormGroup } from '@angular/forms';
import { ClipboardService } from 'ngx-clipboard';

@Component({
  selector: 'cbc-share',
  templateUrl: './share.component.html',
  styleUrls: ['./share.component.scss'],
  preserveWhitespaces: true,
})
export class ShareComponent implements OnInit {
  @Input()
  showLabel = true;
  @Input()
  orientationRight = true;
  @Input()
  enableDirectDownload = false;
  @Input()
  link!: string;
  @Input()
  color: 'grey' | 'blue' = 'blue'; // grey or blue

  public showBox = false;
  public copied = false;
  public routeLink = window.location.href;
  public customisationForm!: FormGroup;

  public constructor(
    private clipboardService: ClipboardService,
    private formBuilder: FormBuilder
  ) {}

  ngOnInit(): void {
    this.customisationForm = this.formBuilder.group({
      addDownload: [false],
    });
  }

  public isCopyAvailable() {
    return this.clipboardService.isSupported;
  }

  toggle() {
    this.showBox = !this.showBox;
    this.routeLink = window.location.href;

    this.customisationForm.controls.addDownload.setValue(
      this.routeLink.includes('download=true')
    );
  }

  hide() {
    this.showBox = false;
  }

  getLink() {
    let result = '';
    if (this.link) {
      result = this.link;
    } else {
      result = this.routeLink;
    }

    if (result.indexOf('download=') !== -1) {
      result = result.substring(0, result.indexOf('download=') - 1);
    }

    if (this.customisationForm.value.addDownload) {
      result = `${result}${
        result.indexOf('?') !== -1 ? '&download=true' : '?download=true'
      }`;
    }

    return result;
  }

  get imageLink(): string {
    return `img/icon-share-${this.color}.png`;
  }

  public showSnackBar() {
    this.copied = true;

    setTimeout(() => {
      this.copied = false;
      // eslint-disable-next-line @typescript-eslint/indent
    }, 1000);
  }
}
