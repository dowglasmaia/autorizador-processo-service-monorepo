import { TestBed } from '@angular/core/testing';
import { environment } from '../../../environments/environment';
import { API_BASE_URL } from './api-base-url.token';

describe('API_BASE_URL', () => {
  it('lê apiBaseUrl do environment', () => {
    expect(TestBed.inject(API_BASE_URL)).toBe(environment.apiBaseUrl);
  });
});
