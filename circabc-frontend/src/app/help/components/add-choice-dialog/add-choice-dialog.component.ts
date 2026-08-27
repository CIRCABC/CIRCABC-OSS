import { Component, ChangeDetectionStrategy, inject } from '@angular/core';
import { MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { TranslocoModule } from '@jsverse/transloco';

export type AddChoiceType = 'article' | 'subcategory';

@Component({
  selector: 'cbc-add-choice-dialog',
  standalone: true,
  imports: [MatDialogModule, TranslocoModule],
  templateUrl: './add-choice-dialog.component.html',
  styleUrl: './add-choice-dialog.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AddChoiceDialogComponent {
  private readonly dialogRef = inject(MatDialogRef<AddChoiceDialogComponent>);

  onArticleClick(): void {
    this.dialogRef.close('article');
  }

  onSubcategoryClick(): void {
    this.dialogRef.close('subcategory');
  }

  onCancel(): void {
    this.dialogRef.close();
  }
}
