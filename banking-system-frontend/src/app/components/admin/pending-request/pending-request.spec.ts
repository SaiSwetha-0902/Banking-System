import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PendingRequest } from './pending-request';

describe('PendingRequest', () => {
  let component: PendingRequest;
  let fixture: ComponentFixture<PendingRequest>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PendingRequest]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PendingRequest);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
