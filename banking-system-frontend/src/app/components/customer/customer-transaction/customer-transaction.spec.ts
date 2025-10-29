import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CustomerTransaction } from './customer-transaction';

describe('CustomerTransaction', () => {
  let component: CustomerTransaction;
  let fixture: ComponentFixture<CustomerTransaction>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CustomerTransaction]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CustomerTransaction);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
