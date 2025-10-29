import { TestBed } from '@angular/core/testing';

import { Accountservice } from './accountservice';

describe('Accountservice', () => {
  let service: Accountservice;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Accountservice);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
