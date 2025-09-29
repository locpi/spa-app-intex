import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SpaControllerComponent } from './components/spa-controller/spa-controller.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SpaControllerComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('Spa Control Center');
}
