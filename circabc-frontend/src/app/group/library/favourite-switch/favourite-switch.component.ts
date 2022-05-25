import { Component, Input, OnInit } from '@angular/core';

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
  styleUrls: ['./favourite-switch.component.scss'],
  preserveWhitespaces: true,
})
export class FavouriteSwitchComponent implements OnInit {
  @Input()
  node!: ModelNode;
  private isWorking = false;
  public shaw = false;

  constructor(
    private favouritesService: FavouritesService,
    private loginService: LoginService
  ) {}

  ngOnInit(): void {
    this.shaw = false;
    if (!this.loginService.isGuest() && this.node.type) {
      this.shaw =
        this.node.type.indexOf('filelink') === -1 &&
        this.node.type.indexOf('folderlink') === -1;
    }
  }

  async toggleFav() {
    if (this.isWorking) {
      return;
    }
    try {
      this.isWorking = true;
      if (this.node) {
        if (this.node.favourite && this.node.id) {
          await firstValueFrom(
            this.favouritesService.deleteFavourite(
              this.loginService.getCurrentUsername(),
              this.node.id
            )
          );
          this.node.favourite = false;
        } else {
          const body: SimpleId = { id: this.node.id as string };
          await firstValueFrom(
            this.favouritesService.postFavourite(
              this.loginService.getCurrentUsername(),
              body
            )
          );
          this.node.favourite = true;
        }
      }
    } finally {
      this.isWorking = false;
    }
  }

  isFavourite(): boolean {
    if (this.node) {
      if (this.node.favourite) {
        return this.node.favourite;
      }
    }

    return false;
  }
}
