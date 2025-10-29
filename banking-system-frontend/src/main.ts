import { bootstrapApplication } from '@angular/platform-browser';
import { App } from './app/app';
import { appConfig } from './app/app.config';
import { provideHttpClient, withInterceptors } from '@angular/common/http';
import { jwtInterceptor } from './app/interceptor/jwt-interceptor';
import { importProvidersFrom } from '@angular/core';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
bootstrapApplication(App, {
  
  ...appConfig,
  providers: [
    importProvidersFrom(BrowserAnimationsModule),
    ...(appConfig.providers || []),
    provideHttpClient(withInterceptors([jwtInterceptor])),
    
  ]
}).catch(err => console.error(err));
