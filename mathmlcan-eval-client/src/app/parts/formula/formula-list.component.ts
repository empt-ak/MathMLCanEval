import {Component, OnInit} from '@angular/core';
import {BaseComponent} from '../../shared/base.component';
import {IPageChangeEvent, TdMediaService} from '@covalent/core';
import {FormulaService} from './formula.service';
import {FormulaResponse} from '../../models/formula.response';
import {QueryParamsBuilder} from '../../shared/query-params-builder';
import {CollectionsService} from '../collections/collections.service';
import {MatDialog} from '@angular/material';
import {NewCollectionComponent} from '../collections/new-collection.component';
import {FormulaCollectionNew} from '../../models/formula-collection.new';

@Component({
  selector: 'formula-list',
  templateUrl: 'formula-list.component.html',
  styleUrls: ['formula-list.component.css']
})
export class FormulaListComponent extends BaseComponent implements OnInit {
  selectMode = false;
  eventResponsive: IPageChangeEvent;
  pageSizeResponsive: number = 25;
  formulas: FormulaResponse[];
  selectedFormulas = new Set();

  constructor(private formulaService: FormulaService,
              public media: TdMediaService,
              private dialog: MatDialog,
              private collectionsService: CollectionsService) {
    super();
  }

  ngOnInit(): void {
    this.formulaService
    .query(new QueryParamsBuilder().withPage(2).build())
    .subscribe((res: FormulaResponse[]) => this.formulas = res);
  }


  changeResponsive(event: IPageChangeEvent): void {
    console.log(event);
    let qpb = new QueryParamsBuilder()
    .withPage(event.page);

    this.formulaService
    .query(qpb.build())
    .subscribe((res: FormulaResponse[]) => this.formulas = res);

    this.eventResponsive = event;
  }

  switchSelectMode(): void {
    this.selectMode = !this.selectMode;
    console.log(`FormulaListComponent@ is in select mode : ${this.selectMode}`);
  }

  formulaClicked(id: number, add: boolean) {
    if (add) {
      this.selectedFormulas.add(id);
      console.log(`FormulaListComponent@ Formula ${id} selected`);
    } else {
      this.selectedFormulas.delete(id);
      console.log(`FormulaListComponent@ Formula ${id} unselected`);
    }
  }

  displayNewCollectionModal(): void {
    const ref = this.dialog.open(NewCollectionComponent,{
      data: {selectedFormulas: this.selectedFormulas}
    });

    ref.afterClosed().subscribe((col: FormulaCollectionNew) => console.log(col));
  }
}