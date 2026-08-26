import { Component, OnInit, input } from '@angular/core';

import {
  FavouritesService,
  Node as ModelNode,
  SimpleId,
} from 'app/core/generated/circabc';
import { LoginService } from 'app/core/login.service';
import { firstValueFrom } from 'rxjs';

@Component({
  selector: 'cbc-favourite-switch',
  templateUrl: './favourite-switch.component.html',
  styleUrl: './favourite-switch.component.scss',
  preserveWhitespaces: true,
})
export class FavouriteSwitchComponent implements OnInit {
  readonly node = input.required<ModelNode>();
  private isWorking = false;
  public shaw = false;

  constructor(
    private favouritesService: FavouritesService,
    private loginService: LoginService
  ) {}

  ngOnInit(): void {
    this.shaw = false;
    const node = this.node();
    if (!this.loginService.isGuest() && node.type) {
      this.shaw =
        node.type.indexOf('filelink') === -1 &&
        node.type.indexOf('folderlink') === -1;
    }
  }

  async toggleFav() {
    if (this.isWorking) {
      return;
    }
    try {
      this.isWorking = true;
      const node = this.node();
      if (node) {
        if (node.favourite && node.id) {
          await firstValueFrom(
            this.favouritesService.deleteFavourite(
              this.loginService.getCurrentUsername(),
              node.id
            )
          );
          node.favourite = false;
        } else {
          const body: SimpleId = { id: node.id as string };
          await firstValueFrom(
            this.favouritesService.postFavourite(
              this.loginService.getCurrentUsername(),
              body
            )
          );
          node.favourite = true;
        }
      }
    } finally {
      this.isWorking = false;
    }
  }

  isFavourite(): boolean {
    const node = this.node();
    if (node) {
      if (node.favourite) {
        return node.favourite;
      }
    }

    return false;
  }
}
