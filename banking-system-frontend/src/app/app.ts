
import { Component, signal } from '@angular/core';

import { RouterOutlet } from '@angular/router';
import { Header } from './components/header/header';


@Component({
  selector: 'app-root',
  imports: [ RouterOutlet,Header],
  templateUrl: './app.html',
  styleUrls: ['./app.css'],  // ← fixed
})
export class App {
  protected readonly title = signal('banking-system-frontend');
}
