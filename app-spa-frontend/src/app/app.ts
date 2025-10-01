import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SpaControllerComponent } from './components/spa-controller/spa-controller.component';
import { ConnectionTestComponent } from './components/connection-test/connection-test.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, SpaControllerComponent, ConnectionTestComponent],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  protected readonly title = signal('Spa Control Center');
  showConnectionTest = signal(true);

  toggleConnectionTest() {
    this.showConnectionTest.set(!this.showConnectionTest());
  }
}
